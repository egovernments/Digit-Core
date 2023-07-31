import React from "react";
import { SportsRugby } from "./SportsRugby";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsRugby",
  component: SportsRugby,
};

export const Default = () => <SportsRugby />;
export const Fill = () => <SportsRugby fill="blue" />;
export const Size = () => <SportsRugby height="50" width="50" />;
export const CustomStyle = () => <SportsRugby style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsRugby className="custom-class" />;

export const Clickable = () => <SportsRugby onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsRugby {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
