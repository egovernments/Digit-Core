import React from "react";
import { LastPage } from "./LastPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LastPage",
  component: LastPage,
};

export const Default = () => <LastPage />;
export const Fill = () => <LastPage fill="blue" />;
export const Size = () => <LastPage height="50" width="50" />;
export const CustomStyle = () => <LastPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LastPage className="custom-class" />;

export const Clickable = () => <LastPage onClick={()=>console.log("clicked")} />;

const Template = (args) => <LastPage {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
