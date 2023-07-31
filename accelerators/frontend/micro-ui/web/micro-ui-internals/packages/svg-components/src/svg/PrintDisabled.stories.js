import React from "react";
import { PrintDisabled } from "./PrintDisabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PrintDisabled",
  component: PrintDisabled,
};

export const Default = () => <PrintDisabled />;
export const Fill = () => <PrintDisabled fill="blue" />;
export const Size = () => <PrintDisabled height="50" width="50" />;
export const CustomStyle = () => <PrintDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PrintDisabled className="custom-class" />;

export const Clickable = () => <PrintDisabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <PrintDisabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
