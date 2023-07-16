import React from "react";
import { Support } from "./Support";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Support",
  component: Support,
};

export const Default = () => <Support />;
export const Fill = () => <Support fill="blue" />;
export const Size = () => <Support height="50" width="50" />;
export const CustomStyle = () => <Support style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Support className="custom-class" />;
export const Clickable = () => <Support onClick={()=>console.log("clicked")} />;

const Template = (args) => <Support {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
