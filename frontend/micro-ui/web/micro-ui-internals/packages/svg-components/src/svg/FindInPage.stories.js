import React from "react";
import { FindInPage } from "./FindInPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FindInPage",
  component: FindInPage,
};

export const Default = () => <FindInPage />;
export const Fill = () => <FindInPage fill="blue" />;
export const Size = () => <FindInPage height="50" width="50" />;
export const CustomStyle = () => <FindInPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FindInPage className="custom-class" />;

export const Clickable = () => <FindInPage onClick={()=>console.log("clicked")} />;

const Template = (args) => <FindInPage {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
