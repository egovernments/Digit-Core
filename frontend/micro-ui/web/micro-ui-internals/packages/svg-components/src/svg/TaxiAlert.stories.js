import React from "react";
import { TaxiAlert } from "./TaxiAlert";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TaxiAlert",
  component: TaxiAlert,
};

export const Default = () => <TaxiAlert />;
export const Fill = () => <TaxiAlert fill="blue" />;
export const Size = () => <TaxiAlert height="50" width="50" />;
export const CustomStyle = () => <TaxiAlert style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TaxiAlert className="custom-class" />;
export const Clickable = () => <TaxiAlert onClick={()=>console.log("clicked")} />;

const Template = (args) => <TaxiAlert {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
